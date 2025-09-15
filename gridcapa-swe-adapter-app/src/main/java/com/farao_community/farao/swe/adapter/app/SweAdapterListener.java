/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.swe.adapter.app;

import com.farao_community.farao.gridcapa.task_manager.api.ProcessRunDto;
import com.farao_community.farao.gridcapa_swe_commons.exception.SweInvalidDataException;
import com.farao_community.farao.gridcapa_swe_commons.resource.ProcessType;
import com.farao_community.farao.gridcapa.task_manager.api.ProcessFileDto;
import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;
import com.farao_community.farao.gridcapa.task_manager.api.TaskStatus;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
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
    private final MinioAdapter minioAdapter;

    @Value("${swe-adapter.process-type}")
    private String processType;

    public SweAdapterListener(final SweClient sweClient,
                              final MinioAdapter minioAdapter) {
        this.sweClient = sweClient;
        this.minioAdapter = minioAdapter;
    }

    @Bean
    public Consumer<TaskDto> consumeTask() {
        return this::handleManualTask;
    }

    private void handleManualTask(final TaskDto taskDto) {
        try {
            if (isTaskReadyForManualProcessing(taskDto)) {
                LOGGER.info("Handling manual run request on TS {} ", taskDto.getTimestamp());
                final SweRequest request = getManualSweRequest(taskDto);
                CompletableFuture.runAsync(() -> sweClient.run(request, SweRequest.class));
            } else {
                LOGGER.warn("Failed to handle manual run request on timestamp {} because it is not ready yet", taskDto.getTimestamp());
            }
        } catch (final Exception e) {
            throw new SweAdapterException(String.format("Error during handling manual run request %s on TS ", taskDto.getTimestamp()), e);
        }
    }

    private static boolean isTaskReadyForManualProcessing(final TaskDto taskDto) {
        return taskDto.getStatus() == TaskStatus.READY
               || taskDto.getStatus() == TaskStatus.SUCCESS
               || taskDto.getStatus() == TaskStatus.INTERRUPTED
               || taskDto.getStatus() == TaskStatus.ERROR;
    }

    SweRequest getManualSweRequest(final TaskDto taskDto) {
        final List<ProcessFileDto> inputs = taskDto.getInputs();
        return new SweRequest(taskDto.getId().toString(),
                getCurrentRunId(taskDto),
                getProcessTypeFromConfiguration(),
                taskDto.getTimestamp(),
                getFileRessourceFromInputs(inputs, "CORESO_SV"),
                getFileRessourceFromInputs(inputs, "REE_EQ"),
                getFileRessourceFromInputs(inputs, "REE_SSH"),
                getFileRessourceFromInputs(inputs, "REE_TP"),
                getFileRessourceFromInputs(inputs, "REN_EQ"),
                getFileRessourceFromInputs(inputs, "REN_SSH"),
                getFileRessourceFromInputs(inputs, "REN_TP"),
                getFileRessourceFromInputs(inputs, "RTE_EQ"),
                getFileRessourceFromInputs(inputs, "RTE_SSH"),
                getFileRessourceFromInputs(inputs, "RTE_TP"),
                getFileRessourceFromInputs(inputs, "CRAC"),
                getFileRessourceFromInputs(inputs, "BOUNDARY_EQ"),
                getFileRessourceFromInputs(inputs, "BOUNDARY_TP"),
                getFileRessourceFromInputs(inputs, "GLSK"),
                taskDto.getParameters());
    }

    private ProcessType getProcessTypeFromConfiguration() {
        return switch (processType) {
            case "D2CC" -> ProcessType.D2CC;
            case "IDCC" -> ProcessType.IDCC;
            case "IDCC_IDCF" -> ProcessType.IDCC_IDCF;
            case "BTCC" -> ProcessType.BTCC;
            default -> throw new SweInvalidDataException("Unsupported process type");
        };
    }

    private SweFileResource getFileRessourceFromInputs(final List<ProcessFileDto> listInputs,
                                                       final String type) {
        final ProcessFileDto input = listInputs.stream()
                .filter(p -> p.getFilePath() != null)
                .filter(p -> p.getFileType().equals(type))
                .findFirst()
                .orElseThrow(() -> new SweAdapterException("No file found for type " + type));
        return new SweFileResource(input.getFilename(), minioAdapter.generatePreSignedUrlFromFullMinioPath(input.getFilePath(), 7));
    }

    private String getCurrentRunId(final TaskDto taskDto) {
        final List<ProcessRunDto> runHistory = taskDto.getRunHistory();
        if (runHistory == null || runHistory.isEmpty()) {
            LOGGER.warn("Failed to handle manual run request on timestamp {} because it has no run history", taskDto.getTimestamp());
            throw new SweAdapterException("Failed to handle manual run request on timestamp because it has no run history");
        }
        runHistory.sort((o1, o2) -> o2.getExecutionDate().compareTo(o1.getExecutionDate()));
        return runHistory.getFirst().getId().toString();
    }
}
