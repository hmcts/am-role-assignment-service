provider "azurerm" {
  version = "=1.44.0"
}

locals {
  app_full_name = "${var.product}-${var.component}"

  aseName = "core-compute-${var.env}"
  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  local_ase = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "core-compute-aat" : "core-compute-saat" : local.aseName}"
  env_ase_url = "${local.local_env}.service.${local.local_ase}.internal"

  // Vault name
  previewVaultName = "${var.raw_product}-aat"
  nonPreviewVaultName = "${var.raw_product}-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"

  // Shared Resource Group
  previewResourceGroup = "${var.raw_product}-shared-aat"
  nonPreviewResourceGroup = "${var.raw_product}-shared-${var.env}"
  sharedResourceGroup = "${(var.env == "preview" || var.env == "spreview") ? local.previewResourceGroup : local.nonPreviewResourceGroup}"

  sharedAppServicePlan = "${var.raw_product}-${var.env}"
  sharedASPResourceGroup = "${var.raw_product}-shared-${var.env}"

  // S2S
  s2s_url = "http://rpe-service-auth-provider-${local.env_ase_url}"
  idam_url = "https://idam-api.${local.local_env}.platform.hmcts.net"

  definition_store_host = "http://ccd-definition-store-api-${local.env_ase_url}"
  }

data "azurerm_key_vault" "am_key_vault" {
  name = "${local.vaultName}"
  resource_group_name = "${local.sharedResourceGroup}"
}

data "azurerm_key_vault" "s2s_vault" {
  name = "s2s-${local.local_env}"
  resource_group_name = "rpe-service-auth-provider-${local.local_env}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name = "microservicekey-am-role-assignment-service"
  key_vault_id = "${data.azurerm_key_vault.s2s_vault.id}"
}

resource "azurerm_key_vault_secret" "am_role_assignment_service_s2s_secret" {
  name = "am-role-assignment-service-s2s-secret"
  value = "${data.azurerm_key_vault_secret.s2s_secret.value}"
  key_vault_id = "${data.azurerm_key_vault.am_key_vault.id}"
}

resource "random_string" "draft_encryption_key" {
  length = 16
  special = true
  upper = true
  lower = true
  number = true
  lifecycle {
    ignore_changes = [
      "*"]
  }
}

module "am-role-assignment-service" {
  source = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  product = "${local.app_full_name}"
  location = "${var.location}"
  appinsights_location = "${var.location}"
  env = "${var.env}"
  ilbIp = "${var.ilbIp}"
  subscription = "${var.subscription}"
  is_frontend = false
  common_tags = "${var.common_tags}"
  java_container_version = "9.0"
  appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"
  enable_ase = "${var.enable_ase}"

  app_settings = {
    ENABLE_DB_MIGRATE = "false"
    IDAM_USER_URL = "${local.idam_url}"
    IDAM_S2S_URL = "${local.s2s_url}"
    AM_ROLE_ASSIGNMENT_SERVICE_SECRET = "${data.azurerm_key_vault_secret.s2s_secret.value}"
    DEFINITION_STORE_HOST = "${local.definition_store_host}"
    ROLE_ASSIGNMENT_S2S_AUTHORISED_SERVICES = "${var.authorised-services}"
  }
}
