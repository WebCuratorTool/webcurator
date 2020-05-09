/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.domain.model.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpParser;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.util.EncodingUtil;

import org.archive.format.http.HttpHeaderParser;
import org.archive.format.http.HttpHeaders;
import org.archive.format.warc.WARCConstants;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.arc.ARCRecord;
import org.archive.io.warc.WARCRecord;
import org.archive.io.RecoverableIOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.webcurator.core.util.URLResolverFunc;

/**
 * A data transfer object for passing Arc Harvest file data between the
 * components of the Web Curator Tool.
 *
 * @author bbeaumont
 */
public class ArcHarvestFileDTO {
	/**
	 * The id of the Arc Harvest File.
	 */
	private Long oid;
	/**
	 * the harvest file name.
	 */
	private String name;

	/**
	 * the harvest file type: warc or arc
	 */
	private String type;

	/**
	 * flag to indicate if the ARC file is compressed.
	 */
	private boolean compressed;
	/**
	 * The base directory of the ArcHarvestFile.
	 */
	private String baseDir;
	/**
	 * The harvest result.
	 */
	private HarvestResultDTO harvestResult;
	/**
	 * The maximum URL length to capture
	 */
	public static final int MAX_URL_LENGTH = 1020;

	public static void main(String[] args) throws IOException, ParseException {
		String directory = "/home/leefr/data";
//        String fileName = "IAH-20200129213720923-00000-3549~ide~8443-2.warc";
//        File[] fileList = directory.listFiles(new IndexerBase.ARCFilter());
		String fileName = "nullNLNZ-TI54525977-20100627090034-00001-skynet.arc";

		ArcHarvestFileDTO dto = new ArcHarvestFileDTO();
		dto.setBaseDir(directory);
		dto.setName(fileName);
		dto.index();
	}

	/**
	 * @return true if the ARC file is compressed.
	 */
	public boolean isCompressed() {
		return compressed;
	}

	/**
	 * @param compressed the file compressed flag.
	 */
	public void setCompressed(boolean compressed) {
		this.compressed = compressed;
	}

	/**
	 * @return the id of the ARCHarvestFile.
	 */
	public Long getOid() {
		return oid;
	}

	/**
	 * @param oid the id of the ARCHarvestFile.
	 */
	public void setOid(Long oid) {
		this.oid = oid;
	}

	/**
	 * @return the name of of the ARCHarvestFile.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name of of the ARCHarvestFile.
	 */
	public void setName(String name) {
		this.name = name;
		if (name.toLowerCase().endsWith(WARCConstants.DOT_WARC_FILE_EXTENSION) ||
				name.toLowerCase().endsWith(WARCConstants.DOT_COMPRESSED_WARC_FILE_EXTENSION)) {
			this.type = "warc";
		} else {
			this.type = "arc";
		}
	}


	/**
	 * @return the harvest result data.
	 */
	public HarvestResultDTO getHarvestResult() {
		return harvestResult;
	}

	/**
	 * @param harvestResult the harvest result data.
	 */
	public void setHarvestResult(HarvestResultDTO harvestResult) {
		this.harvestResult = harvestResult;
	}

	/**
	 * @param baseDir the base directory for the Arc Harvest File.
	 */
	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	/**
	 * Create and return the index of the ArcHarvestFile described to by this DTO.
	 *
	 * @return the index Map
	 * @throws IOException    thrown if there is an error
	 * @throws ParseException
	 */
	public Map<String, HarvestResourceDTO> index() throws IOException, ParseException {
		if (this.baseDir == null) {
			throw new IllegalStateException("Cannot index ArcHarvestFile without a base dir");
		}

		return index(new File(this.baseDir));
	}

	public boolean checkIsCompressed() throws IOException {
		ArchiveReader reader = ArchiveReaderFactory.get(new File(baseDir, this.getName()));
		boolean result = reader.isCompressed();
		reader.close();
		return result;
	}

	/**
	 * Create and return the index of the ArcHarvestFile.
	 *
	 * @param baseDir the base directory of the arcs
	 * @throws IOException thrown if there is an error
	 */
	public Map<String, HarvestResourceDTO> index(File baseDir) throws IOException {
		Map<String, HarvestResourceDTO> results = new HashMap<String, HarvestResourceDTO>();

		File theArchiveFile = new File(baseDir, this.getName());
		ArchiveReader reader = ArchiveReaderFactory.get(theArchiveFile);
		this.compressed = reader.isCompressed();

		Iterator<ArchiveRecord> it = reader.iterator();
		while (it.hasNext()) {
			try (ArchiveRecord rec = it.next()) {
				if (rec instanceof WARCRecord) {
					indexWARCResponse(rec, results);
				} else {
					indexARCRecord(rec, results);
				}
			}
		}
		reader.close();

		if (this.type.equals("arc")) {
			repairViaNames(results);
		}

		return results;
	}

	private void repairViaNames(final Map<String, HarvestResourceDTO> results) {
		results.forEach((fromUrl, fromNode) -> {
			ArcHarvestResourceDTO node = (ArcHarvestResourceDTO) fromNode;
			node.getOutlinks().forEach(toUrl -> {
				String formatToUrl = URLResolverFunc.doResolve(fromUrl, null, toUrl);
				if (results.containsKey(formatToUrl)) {
					ArcHarvestResourceDTO toNode = (ArcHarvestResourceDTO) results.get(formatToUrl);
					toNode.setViaName(fromUrl);
				}
			});
			node.clear();
		});
	}

	private void extractLinks(String html, final List<String> outlinks) {
		String[] JSOUP_SELECTORS = {"href", "src"};
		Document doc = Jsoup.parse(html);
		for (String key : JSOUP_SELECTORS) {
			Elements links = doc.select(String.format("[%s]", key));
			links.stream().map(link -> link.attributes().get(key)).forEach(outlinks::add);
			links.clear();
		}
		doc.clearAttributes();
	}

	private void indexARCRecord(ArchiveRecord rec, Map<String, HarvestResourceDTO> results) throws IOException {
		ARCRecord record = (ARCRecord) rec;
		ArchiveRecordHeader header = record.getHeader();


		// If the URL length is too long for the database, skip adding the URL
		// to the index. This ensures that the harvest completes successfully.
		if (header.getUrl().length() > MAX_URL_LENGTH) {
			return;
		}

		ArcHarvestResourceDTO res = new ArcHarvestResourceDTO();
		res.setArcFileName(this.getName());
		res.setName(header.getUrl());
		res.setResourceOffset(header.getOffset());
		res.setCompressed(this.isCompressed());
		res.setStatusCode(record.getStatusCode());

		// Calculate the length.
		long length = header.getLength() - header.getContentBegin();
		res.setLength(length);

		if (header.getMimetype() != null && header.getMimetype().length() > 0) {
			res.setContentType(header.getMimetype());
		}

		String key = URLResolverFunc.doResolve(null, null, res.getName());
		if (key != null) {
			results.put(key, res);
		}

		if (res.getContentType().startsWith("text/html")) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			record.skipHttpHeader();
			record.dump(out);
			String html = out.toString();
			extractLinks(html, res.getOutlinks());
			out.close();
		}
	}

	private void indexWARCResponse(ArchiveRecord rec, Map<String, HarvestResourceDTO> results) throws IOException {
		String mime = rec.getHeader().getMimetype();
		if (mime.equals("text/dns")) {
			return;
		}

		WARCRecord record = (WARCRecord) rec;
		ArchiveRecordHeader header = record.getHeader();

		// If the URL length is too long for the database, skip adding the URL
		// to the index. This ensures that the harvest completes successfully.
		if (header.getUrl() == null || header.getUrl().length() > MAX_URL_LENGTH) {
			return;
		}

		String key = null;
		String warcRecordId = header.getHeaderValue(WARCConstants.HEADER_KEY_ID).toString();
		if (warcRecordId == null) {
			return;
		}
		warcRecordId = warcRecordId.substring(1, warcRecordId.length() - 1);
		if (header.getHeaderValue(WARCConstants.HEADER_KEY_CONCURRENT_TO) != null) {
			String warcConcurrentTo = header.getHeaderValue(WARCConstants.HEADER_KEY_CONCURRENT_TO).toString();
			int lenDelta = warcConcurrentTo.length() - warcRecordId.length() - 2;
			warcConcurrentTo = warcConcurrentTo.substring(1, warcConcurrentTo.length() - 1 - lenDelta);
			key = warcConcurrentTo;
		} else {
			key = warcRecordId;
		}


		ArcHarvestResourceDTO res = null;
		if (results.containsKey(key)) {
			res = (ArcHarvestResourceDTO) results.get(key);
		} else {
			res = new ArcHarvestResourceDTO();
			results.put(key, res);
		}

		String type = rec.getHeader().getHeaderValue(WARCConstants.HEADER_KEY_TYPE).toString();
		if (type.equals(WARCConstants.WARCRecordType.response.toString())) {
			res.setArcFileName(this.getName());
			res.setName(header.getUrl());
			res.setResourceOffset(header.getOffset());
			res.setCompressed(this.isCompressed());

			// need to parse the documents HTTP message and headers here: WARCReader
			// does not implement this...

			byte[] statusBytes = HttpParser.readRawLine(record);
			int eolCharCount = getEolCharsCount(statusBytes);
			if (eolCharCount <= 0) {
				throw new RecoverableIOException("Failed to read http status where one " +
						" was expected: " + new String(statusBytes));
			}
			String statusLine = EncodingUtil.getString(statusBytes, 0,
					statusBytes.length - eolCharCount, WARCConstants.DEFAULT_ENCODING);
			if (!StatusLine.startsWithHTTP(statusLine)) {
				throw new RecoverableIOException("Failed parse of http status line.");
			}
			StatusLine status = new StatusLine(statusLine);
			res.setStatusCode(status.getStatusCode());

			// Calculate the length.
			long length = header.getLength() - header.getContentBegin();
			res.setLength(length);

			HttpHeaders httpHeaders = new HttpHeaderParser().parseHeaders(record);
			String contentType = httpHeaders.getValue(WARCConstants.CONTENT_TYPE);
			if (contentType != null && contentType.length() > 0) {
				res.setContentType(httpHeaders.getValue(WARCConstants.CONTENT_TYPE));
			}
			httpHeaders.clear();
		} else if (type.equals(WARCConstants.WARCRecordType.metadata.toString())) {
			HttpHeaders httpHeaders = new HttpHeaderParser().parseHeaders(record);
			res.setViaName(httpHeaders.getValue("via"));
			res.setSeedFlag(httpHeaders.getValue("seed") != null);
		}
	}

	/**
	 * borrowed(copied) from org.archive.io.arc.ARCRecord...
	 *
	 * @param bytes Array of bytes to examine for an EOL.
	 * @return Count of end-of-line characters or zero if none.
	 */
	private int getEolCharsCount(byte[] bytes) {
		int count = 0;
		if (bytes != null && bytes.length >= 1 &&
				bytes[bytes.length - 1] == '\n') {
			count++;
			if (bytes.length >= 2 && bytes[bytes.length - 2] == '\r') {
				count++;
			}
		}
		return count;
	}

	public void clear() {
		if (this.harvestResult != null) {
			this.harvestResult.clear();
		}
	}
}
