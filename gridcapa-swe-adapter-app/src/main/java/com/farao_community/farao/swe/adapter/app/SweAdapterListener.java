/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.swe.adapter.app;

import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;
import com.farao_community.farao.gridcapa.task_manager.api.TaskStatus;
import com.farao_community.farao.swe.api.resource.SweRequest;
import com.farao_community.farao.swe.api.resource.SweResponse;
import com.farao_community.farao.swe.runner.starter.SweClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author Theo Pascoli {@literal <theo.pascoli at rte-france.com>}
 */
@Component
public class SweAdapterListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SweAdapterListener.class);
    private final SweClient sweClient;

    public SweAdapterListener(SweClient sweClient) {
        this.sweClient = sweClient;
    }

    @Bean
    public Consumer<TaskDto> consumeTask() {
        return this::handleManualTask;
    }

    private void handleManualTask(TaskDto taskDto) {
        try {
            if (taskDto.getStatus() == TaskStatus.READY
                    || taskDto.getStatus() == TaskStatus.SUCCESS
                    || taskDto.getStatus() == TaskStatus.ERROR) {
                LOGGER.info("Handling manual run request on TS {} ", taskDto.getTimestamp());
                SweRequest request = getManualSweRequest(taskDto);
                CompletableFuture.runAsync(() -> sweClient.run(request, SweRequest.class, SweResponse.class));
            } else {
                LOGGER.warn("Failed to handle manual run request on timestamp {} because it is not ready yet", taskDto.getTimestamp());
            }
        } catch (Exception e) {
            throw new SweAdapterException(String.format("Error during handling manual run request %s on TS ", taskDto.getTimestamp()), e);
        }
    }

    SweRequest getManualSweRequest(TaskDto taskDto) {
        return new SweRequest(taskDto.getId().toString());
    }

}
