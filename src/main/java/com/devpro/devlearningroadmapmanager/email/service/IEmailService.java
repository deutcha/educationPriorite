package com.devpro.devlearningroadmapmanager.email.service;

import com.devpro.devlearningroadmapmanager.email.dto.MessageContact;

public interface IEmailService {
    void sendContactMessage(MessageContact message);
}
