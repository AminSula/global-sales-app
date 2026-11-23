package com.aminsula.pilot.sync;

import java.util.concurrent.locks.ReentrantLock;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class SyncController {

    private final SyncService syncService;

    // One refresh at a time: a second click waits for the first to finish
    private final ReentrantLock lock = new ReentrantLock();

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/sync")
    public SyncResult sync() {
        lock.lock();
        try {
            return syncService.refresh();
        } finally {
            lock.unlock();
        }
    }
}
