
output "appServicePlan" {
  value = "${local.app_service_plan}"
}

output "vaultName" {
  value = "${local.key_vault_name}"
}

output "vaultUri" {
  value = "${local.s2s_vault_uri}"
}
