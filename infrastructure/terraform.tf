provider "azurerm" {
  features {}
}

terraform {
  required_version = ">= 0.13"  # Terraform client version
  
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source = "hashicorp/azurerm"
      version = "~> 2.25"
    }
    random = {
      source = "hashicorp/random"
    }
    # null = {
    #   source = "hashicorp/null"
    #   version = "2.1.2"
    # }
    # external = {
    #   source = "hashicorp/external"
    #   version = "1.2.0"
    # }
  }
}