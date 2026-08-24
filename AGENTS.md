# 项目维护说明

## 分支与范围

- `springboot2`、`springboot3`、`springboot4` 分别维护 Spring Boot 2、3、4 版本, 同一功能需要同步到三个分支.
- 只关注 `src` 目录中的正式实现. `ui-react` 是废弃的前端尝试, 不参与维护和发布.
- `api.html` 和 `api-example.html` 保持可读的格式化源码, 不压缩成一行.

## 页面行为

- 接口说明允许包含 HTML, 使用 DOM API 写入节点, 不得把原始说明拼接进 HTML 属性.
- 搜索内容通过节点上的 `data-*` 属性保存. 生成搜索文本时, 将非数字、英文字母、中文的字符替换为空格, 再合并连续空白并转为小写.
- 页面请求参数分为两块: Header 和 Param 共用参数表单, 请求体使用独立文本框. 三者可以同时存在.
- `@RequestHeader` 显示为 Header. 普通参数、`@RequestParam` 和路径参数统一显示为 Param, 页面使用者不需要区分 Path 与 Query.
- `@RequestBody` 决定是否显示请求体输入框, 不能根据请求体示例是否为空决定.
- JSON 请求体使用 JSON 编辑和校验, String 请求体允许纯文本. 请求时使用接口收集到的 Content-Type.
- `@RequestAttribute`、`@SessionAttribute`、`@CookieValue` 不作为用户可输入的请求参数.
- 示例页中的标题使用“请求体示例”, 不使用“请求示例”.
- 说明区域不显示“说明: ”前缀.
- 只要说明或开发信息存在, “响应结果示例地址”前保留一个空行.
- 所有接口必须生成 `exampleUrl`. `WebUtil.getExampleUrl()` 不得返回空字符串, 三个分支都应由 `/api/example/{id}.json` 生成地址.

## 验收

- 使用 `/home/ty/project/github/api-document-example` 做页面验收, 临时修改必须在验收后恢复, 不覆盖该项目原有改动.
- 至少验证 Header + Param + JSON 请求体、Header + Param + 纯文本请求体、multipart 三类组合, 同时检查“示例”和“请求”两个页签.
- 页面验收必须确认 `exampleUrl` 存在, 并检查说明内容和“响应结果示例地址”之间的空行.
- Spring Boot 2 示例项目使用 JDK 8 启动.

## 发布

- 三个分支的版本号分别独立递增, 例如 `2.1.x`、`3.1.x`、`4.1.x`.
- 每个分支发布前执行测试, Spring Boot 2 使用 JDK 8.
- 禁止对三个分支分别执行正式上传, 否则 Maven Central 会记录三次 Release Count.
- 每个分支使用 `mvn clean deploy -P release -DskipPublishing=true` 只生成 `target/central-publishing/central-bundle.zip`.
- 将三个 zip 解压到同一个空目录后重新打包, 最终只向 Central Portal 上传一次, 一个联合 bundle 记为一次 Release Count.
- 上传时使用 `publishingType=AUTOMATIC`, 并等待 deployment 状态变为 `PUBLISHED` 后才算发布完成.
- Maven Central 免费额度当前按组织统计, Usage Center 是额度与使用量的最终依据.
