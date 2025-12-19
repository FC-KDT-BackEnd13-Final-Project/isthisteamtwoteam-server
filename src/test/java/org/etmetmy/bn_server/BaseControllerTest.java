package org.etmetmy.bn_server;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
public abstract class BaseControllerTest {

    @MockBean
    private S3Client s3Client;

    @MockBean
    private JavaMailSender javaMailSender;
}
