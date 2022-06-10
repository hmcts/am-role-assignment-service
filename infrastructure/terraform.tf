provider "azurerm" {
  features {}
}

terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source = "hashicorp/azurerm"
      version = "~> 3.10.0"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "2.22.0"
    }
  }
}
