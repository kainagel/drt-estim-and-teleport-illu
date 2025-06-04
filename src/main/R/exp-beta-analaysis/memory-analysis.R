library(tidyverse)

# Variables 
#identifier <- "/ChangeExpBeta-automated/ChangeExpBeta-sigma_3.0-with_msa/intermediate-results/delta_0.0-gamma_1.0"
#identifier <- "/ChangeExpBeta-mixed-case-test/ChangeExpBeta-sigma_3.0-with_msa/intermediate-results/delta_0.0-gamma_1.0"
identifier <- "/SelectExpBeta-automated/SelectExpBeta-sigma_3.0-with_msa/intermediate-results/delta-0.0"

# common parts of the path
root_folder <- "/Users/luchengqi/Desktop/ChangeExpBeta-test/illu-output/msa-analysis"
ending <- "/memory-analysis.tsv"

# Identify case name for plot title
exp_type <- str_extract(identifier, "(?<=/)(ChangeExpBeta|SelectExpBeta)")
msa_flag <- ifelse(str_detect(identifier, "with_msa"), "with_msa", NULL)
mix_flag <- ifelse(str_detect(identifier, "mixed-case"), "mixed_case", '-')
sigma <- str_extract(identifier, "sigma_\\d+(\\.\\d+)?") %>% str_remove("sigma_")
delta <- str_extract(identifier, "delta[-_]\\d+(\\.\\d+)?") %>% str_remove("delta[-_]")
gamma <- str_extract(identifier, "gamma_\\d+(\\.\\d+)?") %>% str_remove("gamma_")
# Put everything into one string
parts <- c(exp_type, msa_flag, mix_flag, paste0("sigma_", sigma), paste0("delta_", delta))
if (!is.na(gamma)) {
  parts <- c(parts, paste0("gamma_", gamma))
}
title <- paste(parts, collapse = "-")

data_path <- paste0(root_folder, identifier, ending)
memory_analysis_data <- read_delim(data_path, delim = "\t")

expected_vs_actual_data <- memory_analysis_data %>%
  pivot_longer(
    cols = c(expected_final_sto_mode_share, actual_sto_mode_share),
    names_to = "type",
    values_to = "share"
      )

# ggplot(data = memory_analysis_data, aes(x = iteration, y = expected_final_sto_mode_share)) +
#   geom_line() +
#   ylim(0,1)

ggplot(data = expected_vs_actual_data, aes(x = iteration, y = share, colour = type)) +
  geom_line() + 
  ylim(0,1) + 
  ggtitle(title)
