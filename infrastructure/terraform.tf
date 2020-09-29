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
    null = {
      source = "hashicorp/null"
    }
    external = {
      source = "hashicorp/external"
    }
    template = {
      source = "hashicorp/template"
    }
  }
}