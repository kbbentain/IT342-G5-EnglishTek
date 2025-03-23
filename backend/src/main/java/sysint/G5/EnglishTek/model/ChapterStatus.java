package sysint.G5.EnglishTek.model;

public enum ChapterStatus {
    LOCKED,        // Chapter is not yet available
    AVAILABLE,     // Chapter is available but not started
    IN_PROGRESS,   // Chapter has been started but not completed
    COMPLETED      // Chapter is completed
}
