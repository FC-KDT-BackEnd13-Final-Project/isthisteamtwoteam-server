package org.etmetmy.bn_server.domain.project.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.etmetmy.bn_server.domain.checkList.entity.CheckList;
import org.etmetmy.bn_server.domain.file.dto.response.FileInfoDTO;
import org.etmetmy.bn_server.domain.link.dto.LinkInfoDTO;
import org.etmetmy.bn_server.domain.project.entity.ProjectCheckList;

import java.util.List;

@Getter
@Builder
public class ProjectCheckListAllResponse {
    private Long id;
    private Long projectId;
    private Long checkListId;
    private String checkListContent;
    private boolean checked;
    private List<FileInfoDTO> files;
    private List<LinkInfoDTO> links;

    public static class Converter{
        public static ProjectCheckListAllResponse from(
                ProjectCheckList projectCheckList,
                CheckList checkList,
                List<FileInfoDTO> files,
                List<LinkInfoDTO> links) {
         return ProjectCheckListAllResponse.builder()
                 .id(projectCheckList.getProjectCheckListId())
                 .projectId(projectCheckList.getProject().getId())
                 .checkListId(projectCheckList.getCheckListId())
                 .checkListContent(checkList.getContent())
                 .checked(projectCheckList.getChecked())
                 .files(files)
                 .links(links)
                 .build();
        }
    }
}
