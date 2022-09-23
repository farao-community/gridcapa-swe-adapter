package com.farao_community.farao.swe.adapter.app;

import com.farao_community.farao.gridcapa.task_manager.api.*;
import com.farao_community.farao.swe.runner.api.resource.SweRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Theo Pascoli {@literal <theo.pascoli at rte-france.com>}
 */
@SpringBootTest
class SweAdapterListenerTest {

    @Autowired
    private SweAdapterListener sweAdapterListener;

    private static String cgmFileName;
    private static String cgmFileType;
    private static String cgmFileUrl;

    @BeforeAll
    static void setUp() {
        cgmFileName = "cgm";
        cgmFileType = "CGM";
        cgmFileUrl = "file://cgm.uct";
    }

    @Test
    void testGetManualSweRequest() {
        TaskDto taskDto = createTaskDtoWithStatus(TaskStatus.READY);
        SweRequest sweRequest = sweAdapterListener.getManualSweRequest(taskDto);
        assertEquals(taskDto.getId().toString(), sweRequest.getId());
        assertEquals(taskDto.getTimestamp(), sweRequest.getTargetProcessDateTime());
    }

    @Test
    void consumeReadyTask() {
        TaskDto taskDto = createTaskDtoWithStatus(TaskStatus.READY);
        sweAdapterListener.consumeTask().accept(taskDto);
    }

    @Test
    void consumePendingTaskError() {
        TaskDto taskDto = createTaskDtoWithStatus(TaskStatus.PENDING);
        sweAdapterListener.consumeTask().accept(taskDto);
    }

    TaskDto createTaskDtoWithStatus(TaskStatus status) {
        UUID id = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.parse("2022-09-20T10:30Z");
        List<ProcessFileDto> processFiles = new ArrayList<>();
        processFiles.add(new ProcessFileDto(cgmFileType, ProcessFileStatus.VALIDATED, cgmFileName, timestamp, cgmFileUrl));
        List<ProcessEventDto> processEvents = new ArrayList<>();
        return new TaskDto(id, timestamp, status, null, processFiles, null, processEvents);
    }

}
