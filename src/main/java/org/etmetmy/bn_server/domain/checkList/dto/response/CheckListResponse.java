package org.etmetmy.bn_server.domain.checkList.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class CheckListResponse {

    private Long id;
    private String content;
    private Boolean checked;

    public static class Converter{

        public static CheckListResponse from(CheckList checkList){
            return CheckListResponse.builder()
                    .id(checkList.getCheckListId())
                    .content(checkList.getContent())
                    .build();
        }

        public static List<CheckListResponse> from(List<CheckList> checkLists){
            return checkLists.stream()
                    .map(Converter::from)
                    .toList();
        }
    }
}
