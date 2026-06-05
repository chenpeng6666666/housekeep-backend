package com.itxc.housekeepbackend.model.dto.order;

import lombok.Data;
import java.util.List;

@Data
public class BatchDispatchDTO {

    private List<Long> orderIds;
}