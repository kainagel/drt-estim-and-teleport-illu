library(tidyverse)

root_dir <- "/Users/luchengqi/Desktop/ChangeExpBeta-test/illu-output/msa-analysis/ChangeExpBeta-automated"

# Get all folders in the root directory
dirs <- list.dirs(path = root_dir, recursive = FALSE)

# read all the folders and acquire gamma and modeB
df_all <- map_dfr(dirs, function(dir_path) {
  # create file path
  file_path <- file.path(dir_path, "main-stats.tsv")
  if (!file.exists(file_path)) return(NULL)
  
  # read main-stats.tsv
  df <- read_tsv(file_path, show_col_types = FALSE) %>%
    select(delta, sigma, gamma, modeB) %>%
    mutate(sigma = as.character(sigma))
  return(df)
})

ggplot(data = df_all, aes(x = gamma, y = modeB, colour = sigma)) + 
  geom_line() + 
  ylim(0,1) + 
  ggtitle("Final mode share under various gamma value (Change Exp Beta)") +
  theme_minimal(base_size = 16) + # Adjust base font size for academic style
  theme(
    plot.title = element_text(hjust = 0.5, size = 16, face = "bold"), # Center title and increase font size
    axis.title = element_text(size = 14), # Increase axis title font size
    axis.text = element_text(size = 14), # Increase axis text font size
    legend.title = element_text(size = 16, face = "bold"), # Increase legend title font size
    legend.text = element_text(size = 14) # Increase legend text font size
  ) +
  xlab("gamma") + 
  ylab("final Mode B share") + 
  labs(
    colour = "Sigma" # Rename legend title
  )
