library(tidyverse)

# Read in data
# No duplication
base <- read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/base/sigma_0.0-duplicate_0.0/main-stats.tsv") %>%
  rbind(read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/base/sigma_0.1-duplicate_0.0/main-stats.tsv")) %>%
  rbind(read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/base/sigma_0.3-duplicate_0.0/main-stats.tsv")) %>%
  rbind(read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/base/sigma_1.0-duplicate_0.0/main-stats.tsv")) %>%
  rbind(read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/base/sigma_3.0-duplicate_0.0/main-stats.tsv")) %>%
  mutate(sigma = as.character(sigma))

with_duplicate <- read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-duplicate/sigma_0.0-duplicate_0.2/main-stats.tsv") %>%
  rbind(read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-duplicate/sigma_0.1-duplicate_0.2/main-stats.tsv")) %>%
  rbind(read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-duplicate/sigma_0.3-duplicate_0.2/main-stats.tsv")) %>%
  rbind(read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-duplicate/sigma_1.0-duplicate_0.2/main-stats.tsv")) %>%
  rbind(read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-duplicate/sigma_3.0-duplicate_0.2/main-stats.tsv")) %>%
  mutate(sigma = as.character(sigma))

with_preference <- read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-preference/sigma_0.0-duplicate_0.0/main-stats.tsv") %>%
  rbind(read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-preference/sigma_0.1-duplicate_0.0/main-stats.tsv")) %>%
  rbind(read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-preference/sigma_0.3-duplicate_0.0/main-stats.tsv")) %>%
  rbind(read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-preference/sigma_1.0-duplicate_0.0/main-stats.tsv")) %>%
  rbind(read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-preference/sigma_3.0-duplicate_0.0/main-stats.tsv")) %>%
  mutate(sigma = as.character(sigma))


with_preference_with_duplicate <- read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-preference-with-duplicate-1/sigma_0.0-duplicate_0.2/main-stats.tsv") %>%
  rbind(read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-preference-with-duplicate-1/sigma_0.1-duplicate_0.2/main-stats.tsv")) %>%
  rbind(read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-preference-with-duplicate-1/sigma_0.3-duplicate_0.2/main-stats.tsv")) %>%
  rbind(read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-preference-with-duplicate-1/sigma_1.0-duplicate_0.2/main-stats.tsv")) %>%
  rbind(read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-preference-with-duplicate-1/sigma_3.0-duplicate_0.2/main-stats.tsv")) %>%
  mutate(sigma = as.character(sigma))

testing <- read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-preference-std_1/sigma_0.1-duplicate_0.0/main-stats.tsv") %>%
  mutate(sigma = as.character(sigma))

ggplot(data = testing, aes(x = delta, y = drt_mode_share, colour = sigma)) + 
  geom_line()

ggplot(data = base, aes(x = delta, y = drt_mode_share, colour = sigma)) +
  geom_line()+
  ggtitle("Base (no duplication, no preference)")

ggplot(data = with_duplicate, aes(x = delta, y = drt_mode_share, colour = sigma)) +
  geom_line() +
  ggtitle("With plan duplication")

ggplot(data = with_preference, aes(x = delta, y = drt_mode_share, colour = sigma)) +
  geom_line() +
  ggtitle("With preference")

ggplot(data = with_preference_with_duplicate, aes(x = delta, y = drt_mode_share, colour = sigma)) +
  geom_line() +
  ggtitle("With preference, with plan duplication")


### Single case analysis
# Number of DRT plans in memory
num_drt_plans_sig_0 = read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-duplicate/sigma_0.0-duplicate_0.2/intermediate-results/delta-0.0.tsv")
num_drt_plans_sig_03 = read_tsv("/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-duplicate/sigma_0.3-duplicate_0.2/intermediate-results/delta-0.0.tsv")


data_long_sig_03 <- num_drt_plans_sig_03 %>%
  select(-drt_mode_share) %>%
  pivot_longer(cols = -iter, names_to = "variable", values_to = "value") %>%
  mutate(variable = factor(variable, levels = 0:6))  # Set factor levels for ordered stacking

data_long_sig_0 <- num_drt_plans_sig_0 %>%
  select(-drt_mode_share) %>%
  pivot_longer(cols = -iter, names_to = "variable", values_to = "value") %>%
  mutate(variable = factor(variable, levels = 0:6))  # Set factor levels for ordered stacking

ggplot(data_long_sig_0, aes(x = iter, y = value, fill = variable)) +
  geom_area() +
  labs(title = "Num DRT plans, sigma = 0", x = "Iteration", y = "Value") +
  scale_fill_brewer(palette = "Set1", name = "Category") +
  theme_minimal()

ggplot(data_long_sig_03, aes(x = iter, y = value, fill = variable)) +
  geom_area() +
  labs(title = "Num DRT plans, sigma = 0.3", x = "Iteration", y = "Value") +
  scale_fill_brewer(palette = "Set1", name = "Category") +
  theme_minimal()




