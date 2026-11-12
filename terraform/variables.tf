# =============================================================
# Variables de configuración para la infraestructura
# =============================================================

variable "aws_region" {
  description = "Región de AWS donde se despliega la infraestructura"
  type        = string
  default     = "us-east-1"
}

variable "app_name" {
  description = "Nombre de la aplicación (prefijo de recursos)"
  type        = string
  default     = "polizas-app"
}

variable "vpc_cidr" {
  description = "Bloque CIDR de la VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "task_cpu" {
  description = "CPU asignada a la tarea Fargate (unidades vCPU)"
  type        = number
  default     = 256
}

variable "task_memory" {
  description = "Memoria asignada a la tarea Fargate (MiB)"
  type        = number
  default     = 512
}

variable "desired_count" {
  description = "Número de réplicas de la tarea"
  type        = number
  default     = 1
}

variable "spring_profile" {
  description = "Perfil de Spring activo en el contenedor"
  type        = string
  default     = "prod"
}

variable "tags" {
  description = "Etiquetas comunes aplicadas a todos los recursos"
  type        = map(string)
  default = {
    project     = "polizas-app"
    environment = "prod"
    managed_by  = "terraform"
  }
}
