# Java Network Proxy

一个基于Java和Netty实现的网络代理服务器，支持HTTP和SOCKS协议。

## 功能特点

- 支持HTTP代理
- 支持SOCKS4/SOCKS5代理
- 可配置的监听地址和端口
- 详细的日志记录
- 高性能的异步IO处理

## 系统要求

- Java 11或更高版本
- Maven 3.6或更高版本

## 快速开始

1. 克隆项目：
```bash
git clone [repository-url]
cd proxy-server
```

2. 编译项目：
```bash
mvn clean package
```

3. 运行服务器：
```bash
java -jar target/proxy-server-1.0-SNAPSHOT.jar [config.json]
```

## 配置说明

配置文件使用JSON格式，默认配置文件为`config.json`：

```json
{
    "port": 7890,
    "bindAddress": "127.0.0.1",
    "enableHttp": true,
    "enableSocks": true
}
```

- `port`: HTTP代理端口号（SOCKS代理将使用port+1）
- `bindAddress`: 监听地址
- `enableHttp`: 是否启用HTTP代理
- `enableSocks`: 是否启用SOCKS代理

## 使用示例

### HTTP代理
```bash
curl -x http://127.0.0.1:7890 https://www.google.com
```

### SOCKS5代理
```bash
curl --socks5 127.0.0.1:7891 https://www.google.com
```

## 日志

日志文件位于`logs`目录下：
- 控制台输出基本日志信息
- 详细日志记录在`logs/proxy.log`文件中
- 日志文件每天或达到10MB时自动滚动

## 注意事项

- 默认只监听本地地址（127.0.0.1），如需对外提供服务，请修改`bindAddress`
- 确保配置的端口未被其他程序占用
- 建议在生产环境中配置适当的访问控制 