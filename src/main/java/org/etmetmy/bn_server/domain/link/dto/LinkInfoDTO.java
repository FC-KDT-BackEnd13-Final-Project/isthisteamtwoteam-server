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

    @JsonProperty("linkId")
    private Long linkId;

    @JsonProperty("linkUrl")
    private String linkUrl;

    /**
     * Link 엔티티 리스트를 LinkInfo DTO 리스트로 변환
     * 삭제된 링크는 제외
     */
    public static class Converter {
        public static List<LinkInfoDTO> from(List<Link> links) {

            // 1. links가 null 이면 빈 리스트 반환
            if (links == null) {
                return new ArrayList<>();
            }

            // 2. 변환된 LinkInfo를 담을 리스트 생성
            List<LinkInfoDTO> linkInfos = new ArrayList<>();

            // 3. 각 Link를 순회하면서 LinkInfo로 변환
            for (Link link : links) {
                // 삭제된 링크는 건너뛰기
                if (link.getIsDeleted()) {
                    continue;
                }

                // Link 엔티티의 정보를 LinkInfo DTO로 변환
                linkInfos.add(LinkInfoDTO.builder()
                        .linkId(link.getLinkId())
                        .linkUrl(link.getLinkUrl())
                        .build());
            }
            return linkInfos;
        }

        /**
         * Link URL 문자열 리스트를 LinkInfo DTO 리스트로 변환
         * (간단한 URL 리스트만 있을 경우)
         */
        public static List<String> toUrlList(List<Link> links) {
            if (links == null) {
                return new ArrayList<>();
            }

            List<String> urlList = new ArrayList<>();
            for (Link link : links) {
                if (!link.getIsDeleted() && link.getPost() != null) {
                    urlList.add(link.getLinkUrl());
                }
            }
            return urlList;
        }
    }
}