package com.example.gateway.component.blacklist;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
@Slf4j
public class BlackListManager {
    private final ConcurrentSkipListSet<String> blacklist = new ConcurrentSkipListSet<>();

    public boolean isBlocked(String ip) {
        return this.blacklist.contains(ip);
    }

    public void blockIp(String ip) {
        log.info("add ip {} to black list", ip);
        this.blacklist.add(ip);
    }

    public void unblock(String ip) {
        log.info("unblock ip {}", ip);
        this.blacklist.remove(ip);
    }

    public Collection<String> getAll() {
        return this.blacklist;
    }
}
