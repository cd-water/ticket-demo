package com.cdwater.cdticket.app.application;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.app.application.dto.OrderItemVO;
import com.cdwater.cdticket.app.application.dto.OrderRowVO;
import com.cdwater.cdticket.app.application.dto.OrderVO;
import com.cdwater.cdticket.app.application.dto.SeatMapVO;
import com.cdwater.cdticket.app.application.dto.SeatPos;
import com.cdwater.cdticket.app.common.PageResult;
import com.cdwater.cdticket.app.common.ResultCode;
import com.cdwater.cdticket.app.common.exception.BizException;
import com.cdwater.cdticket.app.common.util.SnowflakeIdGenerator;
import com.cdwater.cdticket.app.domain.model.Hall;
import com.cdwater.cdticket.app.domain.model.Order;
import com.cdwater.cdticket.app.domain.model.OrderItem;
import com.cdwater.cdticket.app.domain.model.Screening;
import com.cdwater.cdticket.app.domain.model.SeatConfig;
import com.cdwater.cdticket.app.domain.repository.OrderRepository;
import com.cdwater.cdticket.app.domain.repository.ScreeningRepository;
import com.cdwater.cdticket.app.infrastructure.service.OrderDelayQueueService;
import com.cdwater.cdticket.app.infrastructure.service.SeatLockService;
import com.cdwater.cdticket.app.interfaces.dto.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    public static final int ORDER_TTL_MINUTES = 15;
    public static final String CANCEL_REASON_USER = "用户取消";
    public static final String CANCEL_REASON_EXPIRED = "超时未支付，座位已释放";

    private final OrderRepository orderRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatLockService seatLockService;
    private final OrderDelayQueueService delayQueueService;
    private final SnowflakeIdGenerator idGenerator;

    // ---------- 座位图 ----------

    /** 座位图：已售位图懒初始化（首次从 DB 已支付订单灌入）+ Bitmap 实时状态 */
    public SeatMapVO seatMap(Long screeningId) {
        Screening s = screeningRepository.findById(screeningId);
        if (s == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        Hall hall = screeningRepository.findHall(s.getHallId());
        List<SeatConfig> configs = screeningRepository.findSeatConfigs(hall.getId());
        ensureSoldInitialized(screeningId, hall.getSeatCols());

        List<int[]> positions = configs.stream()
                .map(c -> new int[]{c.getSeatRow(), c.getSeatCol()}).toList();
        List<Integer> statuses = seatLockService.status(screeningId, positions, hall.getSeatCols());

        SeatMapVO vo = new SeatMapVO();
        vo.setScreeningId(s.getId());
        vo.setHallId(hall.getId());
        vo.setHallName(hall.getName());
        vo.setSeatRows(hall.getSeatRows());
        vo.setSeatCols(hall.getSeatCols());
        vo.setSeats(configs.stream().map(c -> {
            SeatMapVO.SeatVO sv = new SeatMapVO.SeatVO();
            sv.setSeatRow(c.getSeatRow());
            sv.setSeatCol(c.getSeatCol());
            sv.setSeatNo(c.getSeatNo());
            boolean disabled = c.getStatus() == null || c.getStatus() != 1;
            sv.setStatus(disabled ? 3 : statuses.get(configs.indexOf(c)));
            return sv;
        }).toList());
        return vo;
    }

    // ---------- 创建订单 ----------

    public OrderVO create(Long userId, CreateOrderRequest req) {
        Screening s = screeningRepository.findById(req.getScreeningId());
        if (s == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        if (!s.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BizException(ResultCode.SCREENING_UNAVAILABLE);
        }
        Hall hall = screeningRepository.findHall(s.getHallId());
        int cols = hall.getSeatCols();

        List<int[]> seats = req.getSeats().stream()
                .map(seat -> new int[]{seat.getSeatRow(), seat.getSeatCol()})
                .distinct().toList();
        Map<String, SeatConfig> template = screeningRepository.findSeatConfigs(hall.getId()).stream()
                .collect(Collectors.toMap(c -> c.getSeatRow() + ":" + c.getSeatCol(), Function.identity()));
        for (int[] p : seats) {
            SeatConfig c = template.get(p[0] + ":" + p[1]);
            if (c == null || c.getStatus() == null || c.getStatus() != 1) {
                throw new BizException(ResultCode.SEAT_INVALID);
            }
        }

        ensureSoldInitialized(s.getId(), cols);
        // Lua 原子检查 + 锁定
        List<int[]> conflicts = seatLockService.lock(seats, s.getId(), cols);
        if (!conflicts.isEmpty()) {
            throw new BizException(ResultCode.SEAT_OCCUPIED,
                    conflicts.stream().map(p -> new SeatPos(p[0], p[1])).toList());
        }

        try {
            Order order = new Order();
            order.setOrderNo(idGenerator.nextId());
            order.setUserId(userId);
            order.setScreeningId(s.getId());
            order.setMovieId(s.getMovieId());
            order.setCinemaId(s.getCinemaId());
            order.setStatus(0);
            order.setTotalAmount(s.getPrice().multiply(BigDecimal.valueOf(seats.size())));
            order.setPayExpireTime(LocalDateTime.now().plusMinutes(ORDER_TTL_MINUTES));
            orderRepository.save(order);

            List<OrderItem> items = seats.stream().map(p -> {
                OrderItem item = new OrderItem();
                item.setOrderId(order.getId());
                item.setScreeningId(s.getId());
                item.setSeatRow(p[0]);
                item.setSeatCol(p[1]);
                item.setSeatNo(template.get(p[0] + ":" + p[1]).getSeatNo());
                item.setPrice(s.getPrice());
                return item;
            }).toList();
            orderRepository.insertItems(items);

            delayQueueService.push(order.getId(), order.getPayExpireTime());
            return toDetailVO(order, items, s, hall, orderRepository.rowById(order.getId()));
        } catch (Exception e) {
            // DB 落库失败补偿释放锁位
            seatLockService.release(seats, s.getId(), cols);
            throw e;
        }
    }

    // ---------- 我的订单 ----------

    public PageResult<OrderVO> page(Long userId, Integer status, int page, int size) {
        IPage<OrderRowVO> p = orderRepository.pageByUser(Page.of(page, size), userId, status);
        Map<Long, List<String>> seatsByOrder = orderRepository
                .itemsByOrderIds(p.getRecords().stream().map(OrderRowVO::getId).toList())
                .stream().collect(Collectors.groupingBy(OrderItem::getOrderId,
                        Collectors.mapping(OrderItem::getSeatNo, Collectors.toList())));
        List<OrderVO> records = p.getRecords().stream()
                .map(r -> toListVO(r, seatsByOrder.getOrDefault(r.getId(), List.of())))
                .toList();
        return new PageResult<>(p.getTotal(), records, p.getCurrent(), p.getSize());
    }

    public OrderVO detail(Long userId, Long id) {
        Order order = requireOwned(userId, id);
        OrderRowVO row = orderRepository.rowById(id);
        List<OrderItem> items = orderRepository.itemsByOrderId(id);
        Screening s = screeningRepository.findById(order.getScreeningId());
        Hall hall = screeningRepository.findHall(s.getHallId());
        return toDetailVO(order, items, s, hall, row);
    }

    public void cancel(Long userId, Long id) {
        Order order = requireOwned(userId, id);
        if (order.getStatus() != 0) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID);
        }
        if (!orderRepository.cancel(id, CANCEL_REASON_USER)) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID); // 并发已支付/已关单
        }
        releaseSeats(order);
        delayQueueService.remove(id);
    }

    // ---------- 内部工具 ----------

    private Order requireOwned(Long userId, Long id) {
        Order order = orderRepository.findById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    /** 释放订单占用的锁位（取消/超时关单用） */
    public void releaseSeats(Order order) {
        List<OrderItem> items = orderRepository.itemsByOrderId(order.getId());
        if (items.isEmpty()) {
            return;
        }
        Screening s = screeningRepository.findById(order.getScreeningId());
        if (s == null) {
            return;
        }
        Hall hall = screeningRepository.findHall(s.getHallId());
        seatLockService.release(toPositions(items), order.getScreeningId(), hall.getSeatCols());
    }

    /** 已售位图懒初始化：首次从 DB 已支付订单灌入（幂等，并发重复执行无害） */
    private void ensureSoldInitialized(Long screeningId, int seatCols) {
        if (seatLockService.soldInitialized(screeningId)) {
            return;
        }
        List<int[]> sold = orderRepository.soldSeatsByScreening(screeningId).stream()
                .map(p -> new int[]{p.getSeatRow(), p.getSeatCol()}).toList();
        if (!sold.isEmpty()) {
            seatLockService.markSold(sold, screeningId, seatCols);
        }
        seatLockService.markSoldInitialized(screeningId);
    }

    private static List<int[]> toPositions(List<OrderItem> items) {
        return items.stream().map(i -> new int[]{i.getSeatRow(), i.getSeatCol()}).toList();
    }

    private static OrderVO toListVO(OrderRowVO row, List<String> seats) {
        OrderVO vo = baseVO(row);
        vo.setSeats(seats);
        return vo;
    }

    private static OrderVO toDetailVO(Order order, List<OrderItem> items, Screening s, Hall hall, OrderRowVO row) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(order.getStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setMovieId(order.getMovieId());
        vo.setScreeningId(order.getScreeningId());
        vo.setCinemaId(order.getCinemaId());
        vo.setHallName(hall.getName());
        vo.setStartTime(s.getStartTime());
        if (row != null) {
            vo.setMovieTitle(row.getMovieTitle());
            vo.setMoviePoster(row.getMoviePoster());
            vo.setCinemaName(row.getCinemaName());
        }
        vo.setItems(items.stream().map(i -> {
            OrderItemVO iv = new OrderItemVO();
            iv.setSeatRow(i.getSeatRow());
            iv.setSeatCol(i.getSeatCol());
            iv.setSeatNo(i.getSeatNo());
            iv.setPrice(i.getPrice());
            return iv;
        }).toList());
        vo.setTicketCode(order.getTicketCode());
        vo.setPayExpireTime(order.getPayExpireTime());
        vo.setPayTime(order.getPayTime());
        vo.setCancelReason(order.getCancelReason());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }

    private static OrderVO baseVO(OrderRowVO row) {
        OrderVO vo = new OrderVO();
        vo.setId(row.getId());
        vo.setOrderNo(row.getOrderNo());
        vo.setStatus(row.getStatus());
        vo.setTotalAmount(row.getTotalAmount());
        vo.setMovieId(row.getMovieId());
        vo.setMovieTitle(row.getMovieTitle());
        vo.setMoviePoster(row.getMoviePoster());
        vo.setCinemaId(row.getCinemaId());
        vo.setCinemaName(row.getCinemaName());
        vo.setHallName(row.getHallName());
        vo.setScreeningId(row.getScreeningId());
        vo.setStartTime(row.getStartTime());
        vo.setTicketCode(row.getTicketCode());
        vo.setPayExpireTime(row.getPayExpireTime());
        vo.setPayTime(row.getPayTime());
        vo.setCancelReason(row.getCancelReason());
        vo.setCreateTime(row.getCreateTime());
        return vo;
    }
}
