library(tidyverse)

mode_share_data <- read_delim(file = "/Users/luchengqi/Desktop/ChangeExpBeta-test/illu-output/msa-analysis/ChangeExpBeta-automated/ChangeExpBeta-sigma_3.0-with_msa/intermediate-results/delta_0.0-gamma_0.1/mode-choice-evolution.tsv", delim = "\t")
#mode_share_data <- read_delim(file = "/Users/luchengqi/Desktop/ChangeExpBeta-test/illu-output/msa-analysis/SelectExpBeta-automated/SelectExpBeta-sigma_3.0/intermediate-results/delta-0.0/mode-choice-evolution.tsv", delim = "\t")

plot_data <- mode_share_data %>%
  pivot_longer(
    cols = c(modeA, modeB),
    names_to = "mode",
    values_to = "share"
  )

ggplot(data = plot_data, aes(x = iter, y = share, colour = mode)) +
  geom_line() +
  ggtitle("Mode share evolution") +
  theme_minimal(base_size = 16) + # Adjust base font size for academic style
  theme(
    plot.title = element_text(hjust = 0.5, size = 16, face = "bold"), # Center title and increase font size
    axis.title = element_text(size = 14), # Increase axis title font size
    axis.text = element_text(size = 14), # Increase axis text font size
    legend.title = element_text(size = 16, face = "bold"), # Increase legend title font size
    legend.text = element_text(size = 14) # Increase legend text font size
  ) +
  xlab("Iterations") + 
  ylab("Mode share") + 
  labs(
    colour = "Mode" # Rename legend title
  )

