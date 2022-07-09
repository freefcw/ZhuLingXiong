package com.example.gateway.module;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.net.InetSocketAddress;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModuleProperty {
    private String name;
    private Integer commandStart;
    private Integer commandEnd;
    private String ip;
    private Integer port;
    private ModuleType type;

    @JsonIgnore
    public InetSocketAddress getAddress() {
        return new InetSocketAddress(this.ip, this.port);
    }

    @JsonIgnore
    public boolean isValid() {
        if (this.name == null) {
            return false;
        }
        if (this.commandStart == null || this.commandEnd == null) {
            return false;
        }

        if (this.commandStart > this.commandEnd) {
            return false;
        }
        if (this.type != ModuleType.INTERNAL) {
            return this.ip != null && this.port != null;
        }
        return true;
    }

    public boolean support(short command) {
        return command >= this.commandStart && command <= this.commandEnd;
    }

    public ModuleType getType() {
        if (this.type == null) {
            return ModuleType.INTERNAL;
        }
        return this.type;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }
}
