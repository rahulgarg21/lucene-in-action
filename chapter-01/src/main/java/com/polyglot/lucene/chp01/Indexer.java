package com.polyglot.lucene.chp01;

import io.vavr.collection.LinkedHashSet;
import io.vavr.collection.Set;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;


/**
 * @author Rajiv Singla . Creation Date: 10/19/2017.
 */
public class Indexer {

    private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

    private static Function<File, Document> fileToDocumentFunction = f -> {
        final Document document = new Document();
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(f.toPath());
            final String contents = new String(bytes, Charset.defaultCharset());
            document.add(new TextField("contents", contents, Field.Store.YES));
            document.add(new StringField("filename", f.getName(), Field.Store.YES));
            document.add(new StringField("fullpath", f.getCanonicalPath(), Field.Store.YES));
        } catch (IOException e) {
            throw new RuntimeException("Unable to add file to Lucene document: " + f);
        }
        return document;
    };


    public static long indexDocuments(final File dataDir, final File indexDir, final FileFilter fileFilter) throws
            IOException {

        final Set<File> filesToIndex = getFilesToIndex(dataDir, fileFilter, LinkedHashSet.empty());
        LOG.debug("Number of files that need to be indexed: {}", filesToIndex.size());

        final Set<Document> documents = filesToIndex.map(fileToDocumentFunction::apply);

        return createFileIndexes(indexDir, documents);
    }


    private static <T extends Document> int createFileIndexes(final File indexDir, final Set<T> entities)
            throws IOException {

        try (final StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
             final Directory directory = FSDirectory.open(indexDir.toPath());
             final IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig(standardAnalyzer))) {
            entities.forEach(document -> {
                try {
                    indexWriter.addDocument(document);
                } catch (IOException ex) {
                    throw new RuntimeException("Unable to add document: " + document + "Exception: " + ex);
                }
            });
            return indexWriter.numDocs();
        }
    }

    private static Set<File> getFilesToIndex(final File dataDir,
                                             final FileFilter fileFilter,
                                             Set<File> indexFileSet) {

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dataDir.toPath())) {
            for (Path path : directoryStream) {
                final File currentFile = path.toFile();
                if (currentFile.isDirectory()) {
                    getFilesToIndex(currentFile, fileFilter, indexFileSet);
                } else {
                    if (currentFile.canRead() && (fileFilter == null || fileFilter.accept(currentFile))) {
                        indexFileSet = indexFileSet.add(currentFile);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to add get Files for Indexing. Exception: " + e);
        }
        return indexFileSet;

    }



}
