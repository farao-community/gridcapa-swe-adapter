/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.swe.adapter.app;

import com.farao_community.farao.gridcapa.task_manager.api.ProcessFileDto;
import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;
import com.farao_community.farao.gridcapa.task_manager.api.TaskStatus;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
import com.farao_community.farao.swe.runner.api.exception.SweInvalidDataException;
import com.farao_community.farao.swe.runner.api.resource.ProcessType;
import com.farao_community.farao.swe.runner.api.resource.SweFileResource;
import com.farao_community.farao.swe.runner.api.resource.SweRequest;
import com.farao_community.farao.swe.runner.starter.SweClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author Theo Pascoli {@literal <theo.pascoli at rte-france.com>}
 */
@Component
public class SweAdapterListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SweAdapterListener.class);
    private final SweClient sweClient;
    private final MinioAdapter minioAadpter;

    @Value("${swe-adapter.process-type}")
    private String processType;

    public SweAdapterListener(SweClient sweClient, MinioAdapter minioAadpter) {
        this.sweClient = sweClient;
        this.minioAadpter = minioAadpter;
    }

    @Bean
    public Consumer<TaskDto> consumeTask() {
        return this::handleManualTask;
    }

    private void handleManualTask(TaskDto taskDto) {
        try {
            if (taskDto.getStatus() == TaskStatus.READY
                    || taskDto.getStatus() == TaskStatus.SUCCESS
                    || taskDto.getStatus() == TaskStatus.INTERRUPTED
                    || taskDto.getStatus() == TaskStatus.ERROR) {
                LOGGER.info("Handling manual run request on TS {} ", taskDto.getTimestamp());
                SweRequest request = getManualSweRequest(taskDto);
                CompletableFuture.runAsync(() -> sweClient.run(request, SweRequest.class));
            } else {
                LOGGER.warn("Failed to handle manual run request on timestamp {} because it is not ready yet", taskDto.getTimestamp());
            }
        } catch (Exception e) {
            throw new SweAdapterException(String.format("Error during handling manual run request %s on TS ", taskDto.getTimestamp()), e);
        }
    }

    SweRequest getManualSweRequest(TaskDto taskDto) {
        return new SweRequest(taskDto.getId().toString(),
                getProcessTypeFromConfiguration(),
                taskDto.getTimestamp(),
                getFileRessourceFromInputs(taskDto.getInputs(), "CORESO_SV"),
                getFileRessourceFromInputs(taskDto.getInputs(), "REE_EQ"),
                getFileRessourceFromInputs(taskDto.getInputs(), "REE_SSH"),
                getFileRessourceFromInputs(taskDto.getInputs(), "REE_TP"),
                getFileRessourceFromInputs(taskDto.getInputs(), "REN_EQ"),
                getFileRessourceFromInputs(taskDto.getInputs(), "REN_SSH"),
                getFileRessourceFromInputs(taskDto.getInputs(), "REN_TP"),
                getFileRessourceFromInputs(taskDto.getInputs(), "RTE_EQ"),
                getFileRessourceFromInputs(taskDto.getInputs(), "RTE_SSH"),
                getFileRessourceFromInputs(taskDto.getInputs(), "RTE_TP"),
                getFileRessourceFromInputs(taskDto.getInputs(), "CRAC"),
                getFileRessourceFromInputs(taskDto.getInputs(), "BOUNDARY_EQ"),
                getFileRessourceFromInputs(taskDto.getInputs(), "BOUNDARY_TP"),
                getFileRessourceFromInputs(taskDto.getInputs(), "GLSK"));
    }

    private ProcessType getProcessTypeFromConfiguration() {
        if (this.processType.equals("D2CC")) {
            return ProcessType.D2CC;
        } else if (this.processType.equals("IDCC")) {
            return ProcessType.IDCC;
        } else {
            throw new SweInvalidDataException("Unsupported process type");
        }
    }

    private SweFileResource getFileRessourceFromInputs(List<ProcessFileDto> listInputs, String type) {
        ProcessFileDto input = listInputs.stream()
                .filter(p -> p.getFilePath() != null)
                .filter(p -> p.getFileType().equals(type))
                .findFirst()
                .orElseThrow(() -> new SweAdapterException("No file found for type " + type));
        return new SweFileResource(input.getFilename(), minioAadpter.generatePreSignedUrlFromFullMinioPath(input.getFilePath(), 1));
    }

}
