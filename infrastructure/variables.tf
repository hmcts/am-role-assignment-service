variable "product" {
  type = string
}

variable "raw_product" {
  type    = string
  default = "am" // jenkins-library overrides product for PRs and adds e.g. pr-123-ia
}

variable "component" {
  type = string
}

variable "location" {
  type    = string
  default = "UK South"
}

variable "env" {
  type = string
}

variable "subscription" {
  type = string
}

variable "ilbIp" {
  type = string
}

variable "common_tags" {
  type = map(string)
}

////////////////////////////////
// Database
////////////////////////////////

variable "postgresql_user" {
  type    = string
  default = "am"
}

variable "database_name" {
  type    = string
  default = "role_assignment"
}

variable "database_sku_name" {
  type    = string
  default = "GP_Gen5_8"
}

variable "database_sku_capacity" {
  default = "8"
}

variable "database_storage_mb" {
  default = "102400"
}