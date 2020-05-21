variable "product" {
  type = "string"
}

variable "raw_product" {
  default = "am" // jenkins-library overrides product for PRs and adds e.g. pr-123-ia
}

variable "component" {
  type = "string"
}

variable "location" {
  type    = "string"
  default = "UK South"
}

variable "env" {
  type = "string"
}

variable "subscription" {
  type = "string"
}

variable "ilbIp" {}

variable "common_tags" {
  type = "map"
}

variable "appinsights_instrumentation_key" {
  default = ""
}

variable "root_logging_level" {
  default = "INFO"
}

variable "log_level_spring_web" {
  default = "INFO"
}

variable "team_name" {
  default     = "AM"
}

variable "managed_identity_object_id" {
  default = ""
}

variable "enable_ase" {
  default = false
}

variable "deployment_namespace" {}


