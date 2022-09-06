/*
 * Copyright (C) 2014-2021 D3X Systems - All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.d3x.morpheus.pipeline;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameAxis;
import com.d3x.morpheus.util.MorpheusException;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

/**
 * Encapsulates a data pipeline along with the data frame dimension to
 * which it should be applied.
 *
 * @author Scott Shaffer
 */
@Value
@AllArgsConstructor
public class FramePipeline {
    /**
     * The axis to which the pipeline should be applied.
     */
    @NonNull
    DataFrameAxis.Type axis;

    /**
     * The data pipeline to apply.
     */
    @NonNull
    DataPipeline pipeline;

    /**
     * The string that separates the axis from the pipeline in encoded
     * strings.
     */
    public static String DELIMITER = ":";

    /**
     * Creates a frame pipeline to be applied to each row of a data frame.
     *
     * @param pipeline the pipeline to be applied.
     *
     * @return a frame pipeline to be applied by row.
     */
    public static FramePipeline byRow(@NonNull DataPipeline pipeline) {
        return new FramePipeline(DataFrameAxis.Type.ROWS, pipeline);
    }

    /**
     * Creates a frame pipeline to be applied to each column of a data frame.
     *
     * @param pipeline the pipeline to be applied.
     *
     * @return a frame pipeline to be applied by column.
     */
    public static FramePipeline byColumn(@NonNull DataPipeline pipeline) {
        return new FramePipeline(DataFrameAxis.Type.COLS, pipeline);
    }

    /**
     * Parses a string containing an encoded frame pipeline.
     *
     * @param str the encoded string.
     *
     * @return the frame pipeline encoded in the string.
     *
     * @throws RuntimeException unless the string is properly formatted.
     */
    public static FramePipeline parse(@NonNull String str) {
        var fields = str.split(DELIMITER);

        if (fields.length != 2)
            throw new MorpheusException("Invalid frame pipeline encoding: [%s].", str);

        var axisStr = fields[0].strip();
        var pipeStr = fields[1].strip();

        return new FramePipeline(DataFrameAxis.Type.valueOf(axisStr), PipelineScanner.scan(pipeStr));
    }

    /**
     * Applies this pipeline to a data frame.
     *
     * @param frame the frame on which to operate.
     *
     * @return the input frame, for operator chaining.
     */
    public <R, C> DataFrame<R, C> apply(@NonNull DataFrame<R, C> frame) {
        switch (axis) {
            case ROWS:
                pipeline.byrow(frame);
                break;

            case COLS:
                pipeline.bycol(frame);
                break;

            default:
                throw new UnsupportedOperationException("Unknown axis type.");
        }

        return frame;
    }

    /**
     * Encodes this frame pipeline into a string.
     *
     * @return a string with this pipeline encoded.
     */
    public String encode() {
        return axis.name() + DELIMITER + pipeline.encode();
    }
}
