/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.swe.adapter.app;

import com.farao_community.farao.gridcapa.task_manager.api.ProcessFileDto;
import com.farao_community.farao.gridcapa.task_manager.api.ProcessFileStatus;
import com.farao_community.farao.gridcapa.task_manager.api.ProcessRunDto;
import com.farao_community.farao.gridcapa.task_manager.api.TaskDto;
import com.farao_community.farao.gridcapa.task_manager.api.TaskStatus;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
import com.farao_community.farao.swe.runner.api.resource.SweRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @author Theo Pascoli {@literal <theo.pascoli at rte-france.com>}
 */
@SpringBootTest
class SweAdapterListenerTest {

    @Autowired
    private SweAdapterListener sweAdapterListener;

    @MockitoBean
    private MinioAdapter minioAdapter;

    @Autowired
    private Consumer<TaskDto> consumeTask;

    @ParameterizedTest
    @CsvSource({"READY, true",
                "SUCCESS, true",
                "INTERRUPTED, true",
                "ERROR, true",
                "CREATED, false"})
    void testHandleManualRequestThroughConsumerBean(final TaskStatus status,
                                                    final boolean shouldBeHandled) {
        final TaskDto taskDto = createTaskDto(true, status);
        when(minioAdapter.generatePreSignedUrlFromFullMinioPath("filePath", 1)).thenReturn("filePathUrl");
        consumeTask.accept(taskDto);
        if (shouldBeHandled) {
            Mockito.verify(minioAdapter, Mockito.times(14)).generatePreSignedUrlFromFullMinioPath("filePath", 1);
        } else {
            Mockito.verifyNoInteractions(minioAdapter);
        }
    }

    @Test
    void testGetManualSweRequest() {
        when(minioAdapter.generatePreSignedUrlFromFullMinioPath("filePath", 1)).thenReturn("filePathUrl");
        TaskDto taskDto = createTaskDto(true, TaskStatus.READY);
        SweRequest sweRequest = sweAdapterListener.getManualSweRequest(taskDto);
        assertEquals(taskDto.getId().toString(), sweRequest.getId());
        assertEquals(taskDto.getTimestamp(), sweRequest.getTargetProcessDateTime());
    }

    @Test
    void testGetManualSweRequestNoRunHistory() {
        when(minioAdapter.generatePreSignedUrlFromFullMinioPath("filePath", 1)).thenReturn("filePathUrl");
        TaskDto taskDto = createTaskDto(false, TaskStatus.READY);
        assertThrows(SweAdapterException.class, () -> sweAdapterListener.getManualSweRequest(taskDto));
    }

    TaskDto createTaskDto(boolean withRunHistory,
                          TaskStatus status) {
        UUID id = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.parse("2022-09-20T10:30Z");
        List<ProcessFileDto> processFiles = new ArrayList<>();
        processFiles.add(new ProcessFileDto("filePath", "CORESO_SV", ProcessFileStatus.VALIDATED, "fileName", "documentId", OffsetDateTime.now()));
        processFiles.add(new ProcessFileDto("filePath", "REE_EQ", ProcessFileStatus.VALIDATED, "fileName", "documentId", OffsetDateTime.now()));
        processFiles.add(new ProcessFileDto("filePath", "REE_TP", ProcessFileStatus.VALIDATED, "fileName", "documentId", OffsetDateTime.now()));
        processFiles.add(new ProcessFileDto("filePath", "REE_SSH", ProcessFileStatus.VALIDATED, "fileName", "documentId", OffsetDateTime.now()));
        processFiles.add(new ProcessFileDto("filePath", "REN_EQ", ProcessFileStatus.VALIDATED, "fileName", "documentId", OffsetDateTime.now()));
        processFiles.add(new ProcessFileDto("filePath", "REN_TP", ProcessFileStatus.VALIDATED, "fileName", "documentId", OffsetDateTime.now()));
        processFiles.add(new ProcessFileDto("filePath", "REN_SSH", ProcessFileStatus.VALIDATED, "fileName", "documentId", OffsetDateTime.now()));
        processFiles.add(new ProcessFileDto("filePath", "RTE_EQ", ProcessFileStatus.VALIDATED, "fileName", "documentId", OffsetDateTime.now()));
        processFiles.add(new ProcessFileDto("filePath", "RTE_TP", ProcessFileStatus.VALIDATED, "fileName", "documentId", OffsetDateTime.now()));
        processFiles.add(new ProcessFileDto("filePath", "RTE_SSH", ProcessFileStatus.VALIDATED, "fileName", "documentId", OffsetDateTime.now()));
        processFiles.add(new ProcessFileDto("filePath", "CRAC", ProcessFileStatus.VALIDATED, "fileName", "documentId", OffsetDateTime.now()));
        processFiles.add(new ProcessFileDto("filePath", "GLSK", ProcessFileStatus.VALIDATED, "fileName", "documentId", OffsetDateTime.now()));
        processFiles.add(new ProcessFileDto("filePath", "BOUNDARY_EQ", ProcessFileStatus.VALIDATED, "fileName", "documentId", OffsetDateTime.now()));
        processFiles.add(new ProcessFileDto("filePath", "BOUNDARY_TP", ProcessFileStatus.VALIDATED, "fileName", "documentId", OffsetDateTime.now()));
        ArrayList<ProcessRunDto> runHistory = null;
        if (withRunHistory) {
            runHistory = new ArrayList<>();
            runHistory.add(new ProcessRunDto(UUID.randomUUID(), OffsetDateTime.now(), Collections.emptyList()));
        }
        return new TaskDto(id, timestamp, status, processFiles, null, null, null, runHistory, Collections.emptyList());
    }

}
