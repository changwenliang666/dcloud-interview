package net.xdclass.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO {
    private String historyBook;
    private String folkHistory;
    private String televisionWork;
    private String commonAnswer;
}
