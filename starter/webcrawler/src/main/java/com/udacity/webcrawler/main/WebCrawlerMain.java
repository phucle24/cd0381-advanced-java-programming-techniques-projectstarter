package com.udacity.webcrawler.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.WebCrawlerModule;
import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.json.CrawlResultWriter;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.Profiler;
import com.udacity.webcrawler.profiler.ProfilerModule;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class WebCrawlerMain {

  private final CrawlerConfiguration config;

  private WebCrawlerMain(CrawlerConfiguration config) {
    this.config = Objects.requireNonNull(config);
  }

  @Inject
  private WebCrawler crawler;

  @Inject
  private Profiler profiler;

  private void run() throws Exception {
    Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);

    CrawlResult result = crawler.crawl(config.getStartPages());
    CrawlResultWriter resultWriter = new CrawlResultWriter(result);
    // TODO: Write the crawl results to a JSON file (or System.out if the file name is empty)
    writeCrawlResults(resultWriter, config.getResultPath());

    // TODO: Write the profile data to a text file (or System.out if the file name is empty)
    writeProfileData(profiler, config.getProfileOutputPath());

  }

  private void writeCrawlResults(CrawlResultWriter resultWriter, String resultPath) throws IOException {
    if (resultPath != null && !resultPath.isEmpty()) {
      // Write to file
      Path pathCrawlResult = Path.of(resultPath);
      resultWriter.write(pathCrawlResult);
    } else {
      // Write to System.out
      try (Writer stdWriterResultPath = new OutputStreamWriter(System.out)) {
        resultWriter.write(stdWriterResultPath);
        stdWriterResultPath.flush();
      }
    }
  }

  private void writeProfileData(Profiler profiler, String profileOutputPath) throws IOException {
    if (profileOutputPath != null && !profileOutputPath.isEmpty()) {
      // Write to file
      Path pathProfileOutput = Path.of(profileOutputPath);
      profiler.writeData(pathProfileOutput);
    } else {
      // Write to System.out
      try (Writer stdWriterProfileOutput = new OutputStreamWriter(System.out)) {
        profiler.writeData(stdWriterProfileOutput);
        stdWriterProfileOutput.flush();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: WebCrawlerMain [starting-url]");
      return;
    }

    CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();
    new WebCrawlerMain(config).run();
  }
}
