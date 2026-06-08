package com.itxc.housekeepbackend.constant;

/**
 * 订单常量
 */
public interface OrderConstant {

    /**
     * 订单状态: 0-待派单, 1-已派单, 2-企业接单, 3-服务中, 4-已完成, 5-已取消
     */
    public static final Integer ORDER_STATUS_WAIT_DISPATCH = 0;
    public static final Integer ORDER_STATUS_DISPATCHED = 1;
    public static final Integer ORDER_STATUS_ACCEPTED = 2;
    public static final Integer ORDER_STATUS_SERVICE = 3;
    public static final Integer ORDER_STATUS_COMPLETE = 4;
    public static final Integer ORDER_STATUS_CANCEL = 5;

}
