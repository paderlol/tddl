package com.taobao.tddl.executor.handler;

import java.util.ArrayList;
import java.util.List;

import com.taobao.tddl.common.exception.TddlException;
import com.taobao.tddl.common.utils.GeneralUtil;
import com.taobao.tddl.executor.ExecutorContext;
import com.taobao.tddl.executor.cursor.ISchematicCursor;
import com.taobao.tddl.executor.record.CloneableRecord;
import com.taobao.tddl.executor.rowset.IRowSet;
import com.taobao.tddl.executor.spi.ExecutionContext;
import com.taobao.tddl.executor.spi.Table;
import com.taobao.tddl.executor.spi.Transaction;
import com.taobao.tddl.executor.utils.ExecUtils;
import com.taobao.tddl.optimizer.config.table.IndexMeta;
import com.taobao.tddl.optimizer.core.plan.IPut;
import com.taobao.tddl.optimizer.core.plan.IPut.PUT_TYPE;

public class DeleteHandler extends PutHandlerCommon {

    public DeleteHandler(){
        super();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected int executePut(ExecutionContext executionContext, IPut put, Table table, IndexMeta meta) throws Exception {

        Transaction transaction = executionContext.getTransaction();
        int affect_rows = 0;
        IPut delete = put;
        ISchematicCursor conditionCursor = null;
        conditionCursor = ExecutorContext.getContext()
            .getTransactionExecutor()
            .execByExecPlanNode(delete.getQueryTree(), executionContext);
        IRowSet rowSet = null;
        try {
            while ((rowSet = conditionCursor.next()) != null) {
                affect_rows++;
                CloneableRecord key = ExecUtils.convertToClonableRecord(rowSet);
                prepare(transaction, table, rowSet, null, null, PUT_TYPE.DELETE);
                table.delete(transaction, key, meta, executionContext.getDbName());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            List<TddlException> exs = new ArrayList();
            exs = conditionCursor.close(exs);

            if (!exs.isEmpty()) throw GeneralUtil.mergeException(exs);
        }
        return affect_rows;
    }

}
