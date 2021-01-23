package com.faeez;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.FlatMapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TypeDescriptors;

import java.util.ArrayList;
import java.util.Collections;

public class StreamingLaptops {

    private static final String header = "Manufacturer,Model Name,Category,Screen Size,Screen,CPU," +
            "RAM, Storage,GPU,Operating System,Operating System Version,Weight,Price (Euros)";

    
    public interface GcsOptions extends PipelineOptions {

        @Description("Path of the file to read from")
        @Validation.Required
        String getInputFile();
        void setInputFile(String value);

        @Description("Name of the table to write to")
        @Default.String("loony-dataflow-review:results.categoryCount")
        String getTableName();
        void setTableName(String value);
    }
    
    
    public static void main(String[] args) {

        GcsOptions options =
                PipelineOptionsFactory.fromArgs(args).withValidation().as(GcsOptions.class);

        readingCsvFile(options);
    }

    private static void readingCsvFile(GcsOptions options) {

        Pipeline pipeline = Pipeline.create(options);

        pipeline.apply("ReadData", TextIO.read().from(options.getInputFile()))

                .apply("FilterHeader", ParDo.of(new FilterHeaderFn(header)))
                .apply("ExtractCategory", FlatMapElements
                        .into(TypeDescriptors.strings())
                        .via((String line) -> Collections.singletonList(line.split(",")[2])))

                .apply("Count", Count.perElement())
                .apply("ConvertToTableRow", ParDo.of(new ConvertToTableRow()))

                .apply("WriteToBigQuery", BigQueryIO.writeTableRows().to(options.getTableName())
                        .withSchema(ConvertToTableRow.getSchema())
                        .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                        .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE));

        pipeline.run().waitUntilFinish();
    }


    private static class FilterHeaderFn extends DoFn<String, String> {

        private static final long serialVersionUID = 1L;

        private final String header;

        public FilterHeaderFn(String header) {
            this.header = header;
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            
            String row = c.element();

            if (!row.isEmpty() && !row.equals(this.header)) {

                c.output(row);
            }
        }
    }


    private static class ConvertToTableRow extends DoFn<KV<String,Long>, TableRow> {

        private static final long serialVersionUID = 1L;

        public static final String category = "category";
        public static final String count = "count";

        @ProcessElement
        public void processElement(DoFn<KV<String,Long>, TableRow>.ProcessContext c) {

            c.output(new TableRow()
                    .set(category, c.element().getKey())
                    .set(count, c.element().getValue())
            );
        }

        static TableSchema getSchema() {

            return new TableSchema().setFields(new ArrayList<TableFieldSchema>() {

                private static final long serialVersionUID = 1L;

                {
                    add(new TableFieldSchema().setName(category).setType("STRING"));
                    add(new TableFieldSchema().setName(count).setType("INTEGER"));
                }
            });
        }
    }
}
