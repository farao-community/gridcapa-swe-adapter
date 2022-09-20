package com.farao_community.farao.swe.adapter.app;

import com.farao_community.farao.gridcapa.task_manager.api.*;
import com.farao_community.farao.swe.api.resource.SweRequest;
import com.farao_community.farao.swe.api.resource.SweResponse;
import com.farao_community.farao.swe.runner.starter.SweClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * @author Theo Pascoli {@literal <theo.pascoli at rte-france.com>}
 */
@SpringBootTest
class SweAdapterListenerTest {

    @Autowired
    private SweAdapterListener sweAdapterListener;

    @Captor
    private ArgumentCaptor<SweRequest> argumentCaptor;

    @MockBean
    private SweClient sweClient;

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
        Assertions.assertEquals(taskDto.getId().toString(), sweRequest.getId());
    }

    @Test
    void consumeReadyTask() {
        TaskDto taskDto = createTaskDtoWithStatus(TaskStatus.READY);
        sweAdapterListener.consumeTask().accept(taskDto);
        Mockito.verify(sweClient).run(argumentCaptor.capture(), eq(SweRequest.class), eq(SweResponse.class));
        SweRequest sweRequest = argumentCaptor.getValue();
        assertNotNull(sweRequest.getId());
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
