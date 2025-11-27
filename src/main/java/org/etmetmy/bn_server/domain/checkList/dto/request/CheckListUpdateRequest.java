package org.etmetmy.bn_server.domain.checkList.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;

import java.util.Optional;

@Getter
@NoArgsConstructor
public class CheckListUpdateRequest {

    private Long checkListId;
    private String content;

    public static class Converter{

        public static CheckList updateEntity(CheckListUpdateRequest request, CheckList checkList){
            return checkList.builder()
                    .checkListId(checkList.getCheckListId())
                    .content(request.getContent())
                    .build();
        }
    }
}
