/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.swe.adapter.app;

import com.farao_community.farao.gridcapa.task_manager.api.*;
import com.farao_community.farao.minio_adapter.starter.MinioAdapter;
import com.farao_community.farao.swe.runner.api.resource.SweRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    @MockBean
    private MinioAdapter minioAdapter;

    @Test
    void testGetManualSweRequest() {
        when(minioAdapter.generatePreSignedUrlFromFullMinioPath("filePath", 1)).thenReturn("filePathUrl");
        TaskDto taskDto = createTaskDto(true);
        SweRequest sweRequest = sweAdapterListener.getManualSweRequest(taskDto);
        assertEquals(taskDto.getId().toString(), sweRequest.getId());
        assertEquals(taskDto.getTimestamp(), sweRequest.getTargetProcessDateTime());
    }

    @Test
    void testGetManualSweRequestNoRunHistory() {
        when(minioAdapter.generatePreSignedUrlFromFullMinioPath("filePath", 1)).thenReturn("filePathUrl");
        TaskDto taskDto = createTaskDto(false);
        assertThrows(SweAdapterException.class, () -> sweAdapterListener.getManualSweRequest(taskDto));
    }

    TaskDto createTaskDto(boolean withRunHistory) {
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
        return new TaskDto(id, timestamp, TaskStatus.READY, processFiles, null, null, null, runHistory, Collections.emptyList());
    }

}
