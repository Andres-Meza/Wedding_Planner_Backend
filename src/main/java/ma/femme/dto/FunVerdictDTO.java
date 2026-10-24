package ma.femme.dto;

import ma.femme.service.FunQuizCatalog;

public record FunVerdictDTO(
    String key,
    String headline,
    String stamp,
    boolean approved,
    String message) {

  public static FunVerdictDTO fromCatalog(FunQuizCatalog.Verdict verdict) {
    return new FunVerdictDTO(
        verdict.key(), verdict.headline(), verdict.stamp(), verdict.approved(), verdict.message());
  }
}
