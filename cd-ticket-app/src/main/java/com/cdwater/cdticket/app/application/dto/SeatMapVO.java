package com.cdwater.cdticket.app.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class SeatMapVO {

    private Long screeningId;
    private Long hallId;
    private String hallName;
    private Integer seatRows;
    private Integer seatCols;
    /** 全量座位 */
    private List<SeatVO> seats;

    @Data
    public static class SeatVO {
        private Integer seatRow;
        private Integer seatCol;
        private String seatNo;
        /** 0 可用 / 1 已售 / 2 锁定中 / 3 不可售 */
        private Integer status;
    }
}
