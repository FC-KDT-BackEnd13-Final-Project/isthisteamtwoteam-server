package org.etmetmy.bn_server.domain.link.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.etmetmy.bn_server.domain.link.entity.Link;

import java.util.ArrayList;
import java.util.List;

/**
 * 링크 정보 DTO (화면 표시용)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkInfoDTO {

    @JsonProperty("linkUrl")
    private String linkUrl;


    public static class Converter {

        public static List<LinkInfoDTO> from(List<Link> links) {
            if (links == null || links.isEmpty()) {
                return List.of();
            }

            return links.stream()
                    .map(link -> LinkInfoDTO.builder()
                            .linkUrl(link.getLinkUrl())
                            .build())
                    .toList();
        }
    }
}