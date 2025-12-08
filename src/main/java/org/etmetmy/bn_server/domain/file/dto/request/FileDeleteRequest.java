package org.etmetmy.bn_server.domain.file.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileDeleteRequest {

    private List<String> fileUrls;
}
