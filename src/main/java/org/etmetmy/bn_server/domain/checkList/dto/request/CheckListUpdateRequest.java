package org.etmetmy.bn_server.domain.checkList.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;

@Getter
@NoArgsConstructor
public class CheckListUpdateRequest {

    private Long checkListId;
    private String content;

    public static class Converter{

        public static void updateEntity(CheckListUpdateRequest request, CheckList checkList){
            checkList.updateContent(request.getContent());
        }

    }
}
