/*
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.swe.adapter.app;

import com.farao_community.farao.gridcapa_swe_commons.exception.AbstractSweException;
import com.farao_community.farao.gridcapa_swe_commons.exception.SweInternalException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Theo Pascoli {@literal <theo.pascoli at rte-france.com>}
 */
class SweExceptionTest {

    @Test
    void checkException() {
        SweAdapterException sweException = new SweAdapterException("Exception message", new Throwable());
        assertEquals("Exception message", sweException.getMessage());

        Exception cause = new RuntimeException("Cause");
        AbstractSweException exception = new SweInternalException("Exception message", cause);
        assertEquals("Exception message", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
