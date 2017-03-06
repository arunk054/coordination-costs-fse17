library(car)
### VIF ###
library("lmerTest")
###Following adopted from https://github.com/aufrank/R-hacks/blob/master/mer-utils.R
vif.mer <- function (fit) {
  ## adapted from rms::vif
  v <- vcov(fit)
  nam <- names(fixef(fit))
  ## exclude intercepts
  ns <- sum(1 * (nam == "Intercept" | nam == "(Intercept)"))
  if (ns > 0) {
    v <- v[-(1:ns), -(1:ns), drop = FALSE]
    nam <- nam[-(1:ns)] }
  d <- diag(v)^0.5
  v <- diag(solve(v/(d %o% d)))
  names(v) <- nam 
  v
}



fit = lmerTest::lmer(time_in_secs_log ~   users_log  + is_external_certain +  is_pullrequest + is_feature + is_contributor_submitted + 
                       crossrefs_log + repo_stars_log + repo_contributors_log + repo_size_kb_log + repo_age_years_log + 
                       is_pullrequest*is_external + is_pullrequest*is_contributor_submitted + is_external*is_contributor_submitted +
                       (1+ is_external|repo) ,  data = df_closed, REML = FALSE )
summary(fit)
r.squaredGLMM(fit)
anova(fit)
vif(fit)
lsmeans(fit, specs = c("is_external_certain"))


fit = lmerTest::lmer(comments_log ~   users_log  + is_external_certain +  is_pullrequest + is_feature + is_contributor_submitted + 
                       crossrefs_log + repo_stars_log + repo_contributors_log + repo_size_kb_log + repo_age_years_log + 
                       is_pullrequest*is_external + is_pullrequest*is_contributor_submitted + is_external*is_contributor_submitted +
                       (1+ is_external|repo) ,  data = df_closed, REML = FALSE )
summary(fit)
r.squaredGLMM(fit)
anova(fit)
vif(fit)
lsmeans(fit, specs = c("is_external_certain"))



fit = lmerTest::lmer(time_in_secs_log ~   users_log + deps_count_log  + depends_on_vetted +  is_pullrequest + is_feature + is_contributor_submitted + 
                       crossrefs_log + repo_stars_log + repo_contributors_log + repo_size_kb_log + repo_age_years_log + 
                       is_pullrequest*depends_on_vetted +     is_pullrequest*is_contributor_submitted + 
                       depends_on_vetted*is_contributor_submitted +
                       (1+ depends_on_vetted|repo) ,  data = df_external, REML = FALSE )

summary(fit)
r.squaredGLMM(fit)
anova(fit)
vif(fit)

fit = lmerTest::lmer(comments_log ~   users_log + deps_count_log  + depends_on_vetted +  is_pullrequest + is_feature + is_contributor_submitted + 
                       crossrefs_log + repo_stars_log + repo_contributors_log + repo_size_kb_log + repo_age_years_log + 
                       is_pullrequest*depends_on_vetted +     is_pullrequest*is_contributor_submitted + 
                       depends_on_vetted*is_contributor_submitted +
                       (1+ depends_on_vetted|repo) ,  data = df_external, REML = FALSE )


summary(fit)
r.squaredGLMM(fit)
anova(fit)
vif(fit)
