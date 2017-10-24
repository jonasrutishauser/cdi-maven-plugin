package com.github.jonasrutishauser.cdi.maven.plugin.weld.services;

import javax.transaction.Synchronization;
import javax.transaction.UserTransaction;

import org.jboss.weld.transaction.spi.TransactionServices;

public final class MockTransactionServices implements TransactionServices {

    @Override
    public void cleanup() {
        // nothing
    }

    @Override
    public void registerSynchronization(Synchronization synchronizedObserver) {
        // nothing
    }

    @Override
    public boolean isTransactionActive() {
        return false;
    }

    @Override
    public UserTransaction getUserTransaction() {
        return null;
    }
}