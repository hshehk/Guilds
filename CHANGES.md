本次只包含需要覆蓋的修改檔案。

修正：
- 移除全部殘留的 Guilds.newChain() 編譯錯誤
- Console backup/migrate 改用 Folia Async + Global Scheduler
- GUI list 改用 Async + Entity Scheduler
- Home warmup 改用 Entity Scheduler
- InfoGUI home warmup 改用 Entity Scheduler
- SchedulerUtils 增加 runGlobal()
- 保持 Paper/Folia 26.2 + Java 25
