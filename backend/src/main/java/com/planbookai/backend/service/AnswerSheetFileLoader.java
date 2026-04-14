package com.planbookai.backend.service;

public interface AnswerSheetFileLoader {

    LoadedAnswerSheetFile load(String fileUrl);

    final class LoadedAnswerSheetFile {
        private final byte[] content;
        private final String mimeType;

        public LoadedAnswerSheetFile(byte[] content, String mimeType) {
            this.content = content;
            this.mimeType = mimeType;
        }

        public byte[] getContent() {
            return content;
        }

        public String getMimeType() {
            return mimeType;
        }
    }
}
