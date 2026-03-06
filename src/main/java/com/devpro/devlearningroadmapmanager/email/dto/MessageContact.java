package com.devpro.devlearningroadmapmanager.email.dto;


import lombok.Data;

import java.time.Instant;

@Data
public class MessageContact {

    private String nom;
    private String email;
    private String sujet;

    private String message;

    private Instant dateEnvoi;
}
