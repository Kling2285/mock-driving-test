package com.example.service.impl;

import com.example.common.VerifyCodeGenerator;
import com.example.entity.VerifyCode;
import com.example.mapper.VerifyCodeMapper;
import com.example.service.EmailSendService;
import com.example.utils.EmailTemplateUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailSendServiceImpl implements EmailSendService {

    @Autowired
    private VerifyCodeMapper verifyCodeMapper;

    @Autowired
    private JavaMailSender javaMailSender;


    @Value("${mail.username}")
    private String fromEmail;

    private final String emailSubject = "【博客平台】您的验证码，请在有效期内使用";
    private final Integer expireMinutes = 10;

    @Override
    public void sendVerifyCodeEmail(String toEmail) {
        String verifyCode = VerifyCodeGenerator.generate6DigitCode();

        VerifyCode codeEntity = new VerifyCode();
        codeEntity.setEmail(toEmail);
        codeEntity.setCode(verifyCode);
        codeEntity.setExpireTime(LocalDateTime.now().plusMinutes(expireMinutes));
        verifyCodeMapper.insertVerifyCode(codeEntity);

        String content = EmailTemplateUtil.generateVerifyCodeContent(verifyCode, expireMinutes);

        sendEmail(toEmail, emailSubject, content);
    }

    @Override
    public boolean checkVerifyCode(String email, String inputCode) {
        VerifyCode verifyCode = verifyCodeMapper.selectUnusedCodeByEmail(email);

        if (verifyCode == null) {
            return false;
        }

        if (!verifyCode.getCode().equals(inputCode)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = verifyCode.getExpireTime();
        if (now.isAfter(expireTime.plusMinutes(1))) {
            return false;
        }

        verifyCodeMapper.updateCodeUsedStatus(verifyCode.getId(), 1);

        return true;
    }

    private void sendEmail(String toEmail, String subject, String content) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, false);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("邮件发送失败：" + e.getMessage());
        }
    }
}