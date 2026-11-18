package com.escrow.backend.project.escrow.exception;

public class EscrowNotFoundException extends RuntimeException {

    public EscrowNotFoundException(Long id) {
        super("Escrow not found with ID: " + id);
    }
}
