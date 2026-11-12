# =============================================================
# Outputs de la infraestructura desplegada
# =============================================================

output "ecr_repository_url" {
  description = "URL del repositorio ECR donde se publica la imagen"
  value       = aws_ecr_repository.app.repository_url
}

output "ecs_cluster_name" {
  description = "Nombre del clúster ECS"
  value       = aws_ecs_cluster.main.name
}

output "ecs_service_name" {
  description = "Nombre del servicio ECS"
  value       = aws_ecs_service.app.name
}

output "cloudwatch_log_group" {
  description = "Grupo de logs de CloudWatch para la aplicación"
  value       = aws_cloudwatch_log_group.app.name
}

output "push_image_commands" {
  description = "Comandos para construir y publicar la imagen en ECR"
  value = join("\n", [
    "aws ecr get-login-password --region ${var.aws_region} | docker login --username AWS --password-stdin ${aws_ecr_repository.app.repository_url}",
    "docker build -t ${var.app_name} .",
    "docker tag ${var.app_name}:latest ${aws_ecr_repository.app.repository_url}:latest",
    "docker push ${aws_ecr_repository.app.repository_url}:latest"
  ])
}
